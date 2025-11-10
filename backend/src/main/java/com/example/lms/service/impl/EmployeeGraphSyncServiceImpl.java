package com.example.lms.service.impl;

import com.example.lms.domain.EmployeeDetails;
import com.example.lms.dto.EmployeeSyncResult;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.service.EmployeeGraphSyncService;
import com.example.lms.service.impl.GraphClientProvider;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class EmployeeGraphSyncServiceImpl implements EmployeeGraphSyncService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeGraphSyncServiceImpl.class);

    private final GraphClientProvider graphClientProvider;
    private final EmployeeDetailsRepository employeeDetailsRepository;

    public EmployeeGraphSyncServiceImpl(GraphClientProvider graphClientProvider,
                                       EmployeeDetailsRepository employeeDetailsRepository) {
        this.graphClientProvider = graphClientProvider;
        this.employeeDetailsRepository = employeeDetailsRepository;
    }

    @Override
    public EmployeeSyncResult syncEmployeeHierarchyFromGraph(String bearerToken, String rootEmailId) {
        logger.info("Starting employee hierarchy sync from Graph API for root email: {}", rootEmailId);

        EmployeeSyncResult result = EmployeeSyncResult.builder()
                .rootEmail(rootEmailId)
                .insertedCount(0)
                .updatedCount(0)
                .skippedCount(0)
                .totalProcessed(0)
                .errors(new ArrayList<>())
                .build();

        try {
            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);

            Set<String> visitedEmails = new HashSet<>();
            Queue<UserToProcess> queue = new LinkedList<>();

            User rootUser = fetchUserDetails(client, rootEmailId);
            if (rootUser == null) {
                result.addError("Root user not found: " + rootEmailId);
                return result;
            }

            String rootEmail = getEmailFromUser(rootUser);
            if (rootEmail == null || rootEmail.trim().isEmpty()) {
                result.addError("Root user has no valid email address");
                return result;
            }

            processAndSaveEmployee(rootUser, null, result, visitedEmails);
            queue.add(new UserToProcess(rootEmail, rootEmail));

            while (!queue.isEmpty()) {
                UserToProcess current = queue.poll();

                try {
                    DirectoryObjectCollectionWithReferencesPage directReports = client
                            .users(current.emailId)
                            .directReports()
                            .buildRequest()
                            .select("displayName,mail,jobTitle,userPrincipalName,id,employeeId")
                            .get();

                    if (directReports != null) {
                        processDirectReportsPage(client, directReports, current.emailId, queue, result, visitedEmails);
                    }

                } catch (Exception e) {
                    logger.error("Error fetching direct reports for {}: {}", current.emailId, e.getMessage(), e);
                    result.addError("Failed to fetch direct reports for " + current.emailId + ": " + e.getMessage());
                }
            }

            logger.info("Employee hierarchy sync completed. Inserted: {}, Updated: {}, Skipped: {}, Total: {}",
                    result.getInsertedCount(), result.getUpdatedCount(), result.getSkippedCount(), result.getTotalProcessed());

        } catch (Exception e) {
            logger.error("Error during employee hierarchy sync: {}", e.getMessage(), e);
            result.addError("Sync failed: " + e.getMessage());
        }

        return result;
    }

    private void processDirectReportsPage(GraphServiceClient<Request> client,
                                         DirectoryObjectCollectionWithReferencesPage directReports,
                                         String managerEmail,
                                         Queue<UserToProcess> queue,
                                         EmployeeSyncResult result,
                                         Set<String> visitedEmails) {
        do {
            if (directReports.getCurrentPage() != null) {
                for (DirectoryObject directoryObject : directReports.getCurrentPage()) {
                    if (directoryObject instanceof User) {
                        User user = (User) directoryObject;
                        String userEmail = getEmailFromUser(user);

                        if (userEmail != null && !userEmail.trim().isEmpty()) {
                            String normalizedEmail = userEmail.toLowerCase().trim();

                            if (!visitedEmails.contains(normalizedEmail)) {
                                processAndSaveEmployee(user, managerEmail, result, visitedEmails);

                                queue.add(new UserToProcess(userEmail, managerEmail));
                                logger.debug("Enqueued employee for hierarchy traversal: {}", userEmail);
                            } else {
                                logger.debug("Skipping already visited email: {}", userEmail);
                                result.setSkippedCount(result.getSkippedCount() + 1);
                            }
                        } else {
                            logger.warn("Skipping user with no valid email: {}", user.displayName);
                            result.addError("User has no valid email: " + user.displayName);
                        }
                    }
                }
            }

            if (directReports.getNextPage() != null) {
                try {
                    directReports = directReports.getNextPage().buildRequest().get();
                } catch (Exception e) {
                    logger.error("Error fetching next page of direct reports: {}", e.getMessage(), e);
                    result.addError("Failed to fetch next page: " + e.getMessage());
                    break;
                }
            } else {
                break;
            }
        } while (directReports != null && directReports.getCurrentPage() != null);
    }

    private void processAndSaveEmployee(User user, String managerEmail, EmployeeSyncResult result, Set<String> visitedEmails) {
        try {
            String email = getEmailFromUser(user);
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Cannot process user with no email: {}", user.displayName);
                return;
            }

            String normalizedEmail = email.toLowerCase().trim();

            if (visitedEmails.contains(normalizedEmail)) {
                logger.debug("Skipping already visited email: {}", email);
                result.setSkippedCount(result.getSkippedCount() + 1);
                return;
            }

            visitedEmails.add(normalizedEmail);

            Optional<EmployeeDetails> existingOpt = employeeDetailsRepository.findByEmailIdIgnoreCase(email);

            EmployeeDetails employee;
            boolean isUpdate = false;

            if (existingOpt.isPresent()) {
                employee = existingOpt.get();
                isUpdate = true;
                logger.debug("Updating existing employee: {}", email);
            } else {
                employee = new EmployeeDetails();
                employee.setEmailId(email);
                employee.setEmpId(getEmployeeIdFromUser(user));
                employee.setEmpActiveFlag("Y");
                employee.setCreatedBy("GRAPH_SYNC");
                employee.setCreatedTs(OffsetDateTime.now());
                logger.debug("Creating new employee: {}", email);
            }

            employee.setEmpName(user.displayName);
            employee.setEmpDesignation(user.jobTitle);
            employee.setRoEmailId(managerEmail);
            employee.setUpdatedBy("GRAPH_SYNC");
            employee.setUpdatedTs(OffsetDateTime.now());

            employeeDetailsRepository.save(employee);

            if (isUpdate) {
                result.setUpdatedCount(result.getUpdatedCount() + 1);
            } else {
                result.setInsertedCount(result.getInsertedCount() + 1);
            }

            result.setTotalProcessed(result.getTotalProcessed() + 1);

            logger.debug("Successfully saved employee: {} ({})", employee.getEmpName(), email);

        } catch (Exception e) {
            logger.error("Error saving employee {}: {}", user.displayName, e.getMessage(), e);
            result.addError("Failed to save employee " + user.displayName + ": " + e.getMessage());
        }
    }

    private User fetchUserDetails(GraphServiceClient<Request> client, String emailId) {
        try {
            logger.debug("Fetching user details for: {}", emailId);
            return client
                    .users(emailId)
                    .buildRequest()
                    .select("displayName,mail,jobTitle,userPrincipalName,id,employeeId")
                    .get();
        } catch (Exception e) {
            logger.error("Error fetching user details for {}: {}", emailId, e.getMessage(), e);
            return null;
        }
    }

    private String getEmailFromUser(User user) {
        if (user.mail != null && !user.mail.trim().isEmpty()) {
            return user.mail.trim();
        }
        if (user.userPrincipalName != null && !user.userPrincipalName.trim().isEmpty()) {
            return user.userPrincipalName.trim();
        }
        return null;
    }

    private Integer getEmployeeIdFromUser(User user) {
        if (user.employeeId != null && !user.employeeId.trim().isEmpty()) {
            try {
                return Integer.parseInt(user.employeeId.trim());
            } catch (NumberFormatException e) {
                logger.warn("Could not parse employeeId '{}' for user {}, using hash instead", user.employeeId, user.displayName);
                return Math.abs(user.employeeId.hashCode());
            }
        }
        logger.warn("No employeeId found for user {}, generating from email hash", user.displayName);
        //String email = getEmailFromUser(user);
        //return email != null ? Math.abs(email.hashCode()) : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        return -1;
    }

    private static class UserToProcess {
        String emailId;
        String managerEmail;

        UserToProcess(String emailId, String managerEmail) {
            this.emailId = emailId;
            this.managerEmail = managerEmail;
        }
    }
}

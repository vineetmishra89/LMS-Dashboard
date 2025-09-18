// src/app/components/course-catalog/catalog-row.model.ts
export interface CatalogRow {
  trainingName: string;
  description: string;
  outline: string;          // outline / content
  duration: string;         // e.g. '15 Hours'
  trainerFeedback?: number; // Trainers current feedback
  userFeedback?: number;    // Current user feedback
  reviewComments?: string;
  trainerName: string;      // Trainer/Name
  prerequisite?: string;    // Pre-requisite
  level?: 'Beginner'|'Intermediate'|'Advanced'|string;
  toolsNeeded?: string;
  recordingLink?: string;   // URL
}

// Example adapter — adjust mappings to your actual Course fields
export function mapCourseToRow(c: any): CatalogRow {
  return {
    trainingName: c.name ?? c.title,
    description: c.description,
    outline: c.outline ?? c.syllabus ?? c.topics,
    duration: c.durationText ?? (c.durationHours ? `${c.durationHours} Hours` : ''),
    trainerFeedback: c.trainerRating,
    userFeedback: c.userRating,
    reviewComments: c.reviewComments,
    trainerName: c.instructorName ?? c.trainer?.name,
    prerequisite: c.prerequisite ?? c.prereq,
    level: c.level,
    toolsNeeded: c.tools ?? c.toolsNeeded,
    recordingLink: c.recordingUrl ?? c.recordingLink
  };
}

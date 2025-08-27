import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from '../../models/certificate';
import { CertificateService } from '../../services/certificate.service';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.scss']
})
export class CertificatesComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  certificates: Certificate[] = [];
  isLoading = true;
  selectedCertificate: Certificate | null = null;
  showCertificateModal = false;
  searchQuery = '';
  filteredCertificates: Certificate[] = [];
  sortBy: 'date' | 'title' | 'course' = 'date';
  
  stats = {
    totalCertificates: 0,
    thisMonth: 0,
    thisYear: 0,
    averageScore: 0
  };

  constructor(
    private certificateService: CertificateService,
    private userService: UserService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadCertificates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCertificates(): void {
    const currentUser = this.userService.getCurrentUser();
    if (!currentUser) return;

    this.isLoading = true;
    
    this.certificateService.getUserCertificates(currentUser.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (certificates) => {
        this.certificates = certificates;
        this.filteredCertificates = certificates;
        this.calculateStats();
        this.applySorting();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load certificates:', error);
        this.isLoading = false;
      }
    });
  }

  private calculateStats(): void {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    
    this.stats = {
      totalCertificates: this.certificates.length,
      thisMonth: this.certificates.filter(c => {
        const certDate = new Date(c.issuedAt);
        return certDate.getMonth() === currentMonth && certDate.getFullYear() === currentYear;
      }).length,
      thisYear: this.certificates.filter(c => {
        const certDate = new Date(c.issuedAt);
        return certDate.getFullYear() === currentYear;
      }).length,
      averageScore: this.certificates.length > 0 
        ? Math.round(this.certificates.reduce((sum, c) => sum + c.finalScore, 0) / this.certificates.length)
        : 0
    };
  }

  onSearch(query: string): void {
    this.searchQuery = query;
    this.filterCertificates();
  }

  private filterCertificates(): void {
    let filtered = this.certificates;
    
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(cert => 
        cert.title.toLowerCase().includes(query) ||
        cert.description.toLowerCase().includes(query)
      );
    }
    
    this.filteredCertificates = filtered;
    this.applySorting();
  }

  onSortChange(sortBy: string): void {
    this.sortBy = sortBy as any;
    this.applySorting();
  }

  private applySorting(): void {
    this.filteredCertificates.sort((a, b) => {
      switch (this.sortBy) {
        case 'date':
          return new Date(b.issuedAt).getTime() - new Date(a.issuedAt).getTime();
        case 'title':
          return a.title.localeCompare(b.title);
        case 'course':
          return a.title.localeCompare(b.title); // Assuming certificate title = course title
        default:
          return 0;
      }
    });
  }

  viewCertificate(certificate: Certificate): void {
    this.selectedCertificate = certificate;
    this.showCertificateModal = true;
  }

  downloadCertificate(certificate: Certificate): void {
    this.certificateService.downloadCertificate(certificate.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${certificate.title.replace(/\s+/g, '_')}_Certificate.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.notificationService.showSuccessNotification('Certificate downloaded successfully!');
      },
      error: (error) => {
        console.error('Download failed:', error);
        this.notificationService.showErrorNotification('Failed to download certificate. Please try again.');
      }
    });
  }

  shareCertificate(certificate: Certificate, platform: string): void {
    this.certificateService.shareCertificate(certificate.id, platform).subscribe({
      next: () => {
        this.notificationService.showSuccessNotification(`Certificate shared on ${platform}!`);
      },
      error: (error) => {
        this.notificationService.showErrorNotification('Failed to share certificate.');
      }
    });
  }

  verifyCertificate(verificationCode: string): void {
    this.certificateService.verifyCertificate(verificationCode).subscribe({
      next: (certificate) => {
        this.notificationService.showSuccessNotification('Certificate verified successfully!');
      },
      error: (error) => {
        this.notificationService.showErrorNotification('Certificate verification failed.');
      }
    });
  }

  closeCertificateModal(): void {
    this.showCertificateModal = false;
    this.selectedCertificate = null;
  }
}
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Certificate } from '../models/certificate';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  constructor(private apiService: ApiService) {}

  getUserCertificates(userId: string): Observable<Certificate[]> {
    return this.apiService.get<Certificate[]>(`users/${userId}/certificates`);
  }

  getCertificateById(certificateId: string): Observable<Certificate> {
    return this.apiService.get<Certificate>(`certificates/${certificateId}`);
  }

  generateCertificate(enrollmentId: string): Observable<Certificate> {
    return this.apiService.post<Certificate>(`enrollments/${enrollmentId}/certificate`, {});
  }

  downloadCertificate(certificateId: string): Observable<Blob> {
    return this.apiService.get<Blob>(`certificates/${certificateId}/download`);
  }

  verifyCertificate(verificationCode: string): Observable<Certificate> {
    return this.apiService.get<Certificate>(`certificates/verify/${verificationCode}`);
  }

  shareCertificate(certificateId: string, platform: string): Observable<any> {
    return this.apiService.post(`certificates/${certificateId}/share`, { platform });
  }
}
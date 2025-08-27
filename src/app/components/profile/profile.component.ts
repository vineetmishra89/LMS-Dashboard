import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';
import { User, UserPreferences } from '../../models/user';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  preferencesForm!: FormGroup;
  currentUser: User | null = null;
  isLoading = false;
  isSaving = false;
  activeTab = 'profile';
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initializeForms();
    this.loadUserData();
  }

  private initializeForms(): void {
    this.profileForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      bio: [''],
      phoneNumber: [''],
      location: [''],
      website: [''],
      socialLinks: this.formBuilder.group({
        linkedin: [''],
        twitter: [''],
        github: ['']
      })
    });

    this.preferencesForm = this.formBuilder.group({
      theme: ['light'],
      language: ['en'],
      notifications: this.formBuilder.group({
        email: [true],
        push: [true],
        courseUpdates: [true],
        achievements: [true],
        weeklyDigest: [false],
        marketingEmails: [false]
      })
    });
  }

  private loadUserData(): void {
    this.isLoading = true;
    
    this.userService.currentUser$.subscribe(user => {
      if (user) {
        this.currentUser = user;
        this.profileForm.patchValue(user);
        this.preferencesForm.patchValue(user.preferences);
        this.previewUrl = user.profileImage;
      }
      this.isLoading = false;
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) { // 5MB limit
        this.notificationService.showErrorNotification('File size must be less than 5MB');
        return;
      }
      
      if (!file.type.startsWith('image/')) {
        this.notificationService.showErrorNotification('Please select an image file');
        return;
      }
      
      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  uploadProfileImage(): void {
    if (!this.selectedFile || !this.currentUser) return;
    
    this.isSaving = true;
    
    this.userService.uploadProfileImage(this.currentUser.id, this.selectedFile).subscribe({
      next: (imageUrl) => {
        this.notificationService.showSuccessNotification('Profile image updated successfully!');
        this.previewUrl = imageUrl;
        this.selectedFile = null;
        this.isSaving = false;
      },
      error: (error) => {
        this.notificationService.showErrorNotification('Failed to upload image. Please try again.');
        this.isSaving = false;
      }
    });
  }

  saveProfile(): void {
    if (this.profileForm.invalid || !this.currentUser) return;
    
    this.isSaving = true;
    const profileData = this.profileForm.value;
    
    this.userService.updateUser(this.currentUser.id, profileData).subscribe({
      next: (updatedUser) => {
        this.notificationService.showSuccessNotification('Profile updated successfully!');
        this.currentUser = updatedUser;
        this.isSaving = false;
      },
      error: (error) => {
        this.notificationService.showErrorNotification('Failed to update profile. Please try again.');
        this.isSaving = false;
      }
    });
  }

  savePreferences(): void {
    if (this.preferencesForm.invalid || !this.currentUser) return;
    
    this.isSaving = true;
    const preferences = this.preferencesForm.value;
    
    this.userService.updatePreferences(this.currentUser.id, preferences).subscribe({
      next: (updatedPreferences) => {
        this.notificationService.showSuccessNotification('Preferences saved successfully!');
        this.isSaving = false;
      },
      error: (error) => {
        this.notificationService.showErrorNotification('Failed to save preferences. Please try again.');
        this.isSaving = false;
      }
    });
  }
}
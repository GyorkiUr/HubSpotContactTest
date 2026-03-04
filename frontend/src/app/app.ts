import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { HubspotService } from './services/hubspot';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  contacts: any[] = [];
  formData = { email: '', firstname: '', lastname: '' };
  statusMessage = '';

  constructor(
    private hubspotService: HubspotService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.refreshList();
  }

  refreshList() {
    this.hubspotService.getContacts().subscribe({
      next: (data) => {
        this.contacts = data && data.results ? data.results : [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Hiba a lista betöltésekor:', err);
        this.statusMessage = 'A szerver jelenleg nem elérhető. Kérjük, próbálja később!';
        this.cdr.detectChanges();
      }
    });
}

  onSubmit() {
    this.hubspotService.upsertContact(this.formData).subscribe({
      next: (res) => {
        this.statusMessage = `Sikeres mentés! Művelet: ${res.action}, ID: ${res.id}`;
        
        this.formData = { email: '', firstname: '', lastname: '' };

        this.refreshList();
      },
      error: (err) => {
        this.statusMessage = err.error?.error || 'Hiba történt a mentés során.';
        this.cdr.detectChanges();
      }
    });
  }
}
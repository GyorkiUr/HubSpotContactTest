import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HubspotService } from './services/hubspot';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule], // HttpClientModule törölve innen
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  contacts: any[] = [];
  formData = { email: '', firstname: '', lastname: '' };
  statusMessage = '';

  constructor(private hubspotService: HubspotService) {}

  ngOnInit() {
    this.refreshList();
  }

  refreshList() {
    this.hubspotService.getContacts().subscribe({
      next: (data) => {
        this.contacts = data.results; // A HubSpot results tömbje
      },
      error: (err) => console.error('Hiba a lista betöltésekor', err)
    });
  }

  onSubmit() {
    this.hubspotService.upsertContact(this.formData).subscribe({
      next: (res) => {
        this.statusMessage = `Siker! Állapot: ${res.action}, ID: ${res.id}`;
        this.formData = { email: '', firstname: '', lastname: '' }; // Form ürítése
        this.refreshList();
      },
      error: (err) => this.statusMessage = 'Hiba történt a mentés során.'
    });
  }
}
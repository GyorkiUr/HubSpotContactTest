import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HubspotService {
  private apiUrl = '/api/contacts';

  constructor(private http: HttpClient) {}

  upsertContact(contact: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, contact);
  }

  getContacts(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }
}
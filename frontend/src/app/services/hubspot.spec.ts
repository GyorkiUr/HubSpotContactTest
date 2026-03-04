import { TestBed } from '@angular/core/testing';

import { Hubspot } from './hubspot';

describe('Hubspot', () => {
  let service: Hubspot;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Hubspot);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

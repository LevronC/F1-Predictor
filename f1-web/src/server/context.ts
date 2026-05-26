import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { dataService } from '../engine/dataService.js';

let initialized = false;

export function ensureServerData(): void {
  if (initialized) return;

  const csvPath = join(process.cwd(), 'public/data.csv');
  const csvText = readFileSync(csvPath, 'utf-8');
  dataService.loadFromCsv(csvText);
  initialized = true;
}

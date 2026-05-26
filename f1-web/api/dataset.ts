import type { VercelRequest, VercelResponse } from '@vercel/node';
import { ensureServerData } from '../src/server/context.js';
import { dataService } from '../src/engine/dataService.js';

export default function handler(_req: VercelRequest, res: VercelResponse) {
  try {
    ensureServerData();
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json(dataService.getDatasetStats());
  } catch (error) {
    res.status(500).json({
      error: error instanceof Error ? error.message : 'Failed to load dataset stats'
    });
  }
}

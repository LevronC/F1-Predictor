import type { VercelRequest, VercelResponse } from '@vercel/node';
import { ensureServerData } from '../src/server/context';
import { dataService } from '../src/engine/dataService';
import { predictionEngine } from '../src/engine/predictionEngine';

export default function handler(_req: VercelRequest, res: VercelResponse) {
  try {
    ensureServerData();
    const limit = 10;
    const predictions = predictionEngine
      .predict(dataService.getAllDrivers(), 74)
      .slice(0, limit);

    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json(predictions);
  } catch (error) {
    res.status(500).json({
      error: error instanceof Error ? error.message : 'Failed to generate predictions'
    });
  }
}

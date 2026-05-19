import type { VercelRequest, VercelResponse } from '@vercel/node';
import { ensureServerData } from '../src/server/context';
import { dataService } from '../src/engine/dataService';
import { runSimulation } from '../src/engine/simulationEngine';

export default function handler(req: VercelRequest, res: VercelResponse) {
  try {
    ensureServerData();

    const rawIterations = Array.isArray(req.query.iterations)
      ? req.query.iterations[0]
      : req.query.iterations;
    const parsed = Number.parseInt(String(rawIterations ?? '1000'), 10);
    const iterations = Number.isFinite(parsed)
      ? Math.min(Math.max(parsed, 100), 5000)
      : 1000;

    const results = runSimulation(dataService.getAllDrivers(), iterations);
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json(results);
  } catch (error) {
    res.status(500).json({
      error: error instanceof Error ? error.message : 'Failed to run simulation'
    });
  }
}

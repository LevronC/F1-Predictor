import type { VercelRequest, VercelResponse } from '@vercel/node';
import { ensureServerData } from '../../src/server/context.js';
import { runBacktest } from '../../src/engine/backtestEngine.js';
import { dataService } from '../../src/engine/dataService.js';

export default function handler(req: VercelRequest, res: VercelResponse) {
  try {
    ensureServerData();

    const rawSeason = Array.isArray(req.query.season) ? req.query.season[0] : req.query.season;
    const season = Number.parseInt(String(rawSeason ?? '2023'), 10);
    if (!Number.isFinite(season)) {
      res.status(400).json({ error: 'Invalid season' });
      return;
    }

    const report = runBacktest(
      dataService.getAllDrivers(),
      dataService.getAllResults(),
      season
    );

    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json(report);
  } catch (error) {
    res.status(500).json({
      error: error instanceof Error ? error.message : 'Failed to run backtest'
    });
  }
}

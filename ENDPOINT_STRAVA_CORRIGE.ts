import { Controller, Get, Query, Redirect } from '@nestjs/common';

@Controller()
export class StravaController {
  @Get('strava/callback')
  @Redirect()
  stravaCallback(
    @Query('code') code?: string,
    @Query('error') error?: string,
    @Query('error_description') errorDescription?: string,
  ) {
    // Log pour déboguer
    console.log('[Strava] Callback received:', { 
      code: code || 'missing', 
      error: error || 'none',
      errorDescription: errorDescription || 'none'
    });
    
    // En cas d'erreur de Strava
    if (error) {
      console.log('[Strava] Error received:', error);
      const errorUrl = `nexofitness://strava/callback?error=${encodeURIComponent(error)}`;
      if (errorDescription) {
        return { url: `${errorUrl}&error_description=${encodeURIComponent(errorDescription)}` };
      }
      return { url: errorUrl };
    }
    
    // Si pas de code (test ou erreur)
    if (!code) {
      console.log('[Strava] No code received - redirecting with error');
      return { url: 'nexofitness://strava/callback?error=no_code' };
    }
    
    // Succès : rediriger vers l'app avec le code d'autorisation
    console.log('[Strava] Redirecting to app with code');
    return { url: `nexofitness://strava/callback?code=${code}` };
  }
}


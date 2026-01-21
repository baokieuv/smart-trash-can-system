import { NextResponse } from 'next/server';
import { BACKEND_API_BASE } from '../../config';

export async function GET(req: Request) {
  try {
    const body = await req.json();
    const { email } = body;

    const response = await fetch(`${BACKEND_API_BASE}/auth/verify-email`, {
      method: 'GET',
    });

    const data = await response.json();

    if (!response.ok) {
      return NextResponse.json(
        { error: data.error || 'Failed to resend verification' },
        { status: response.status }
      );
    }

    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Change password API error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
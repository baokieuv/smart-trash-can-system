import { NextResponse } from 'next/server';
import { BACKEND_API_BASE } from '../../config';

export async function GET(req: Request) {
  try {
    
    const { searchParams } = new URL(req.url);

    const token = searchParams.get('token');

    if(!token){
      return NextResponse.json(
        { error: 'Token is missing' },
        { status: 400 }
      );
    }

    const response = await fetch(`${BACKEND_API_BASE}/auth/verify-email?token=${token}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    const data = await response.json();

    if (!response.ok) {
      return NextResponse.json(
        { error: data.error || 'Failed to verify email' },
        { status: response.status }
      );
    }

    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Verify email API error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
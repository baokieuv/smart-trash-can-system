
import { NextResponse } from 'next/server';
import { BACKEND_API_BASE } from '../../config';

export async function POST(req: Request) {
  try {

    const body = await req.json();
    const { refreshToken } = body;

    if (!refreshToken) {
      return NextResponse.json(
        { error: 'Refresh token is required' },
        { status: 400 }
      );
    }

    // const token = req.headers.get('authorization')?.replace('Bearer ', '');

    // if (!token) {
    //   return NextResponse.json(
    //     { error: 'Unauthorized' },
    //     { status: 401 }
    //   );
    // }

    const response = await fetch(`${BACKEND_API_BASE}/auth/logout`, {
      method: 'POST',
      headers: {
        // 'Authorization': `Bearer ${token}`,
         'Content-Type': 'application/json' 
      },
      body: JSON.stringify({ refreshToken })
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Logout failed' },
        { status: response.status }
      );
    }

    return NextResponse.json({ message: 'Logged out successfully' }, { status: 200 });
  } catch (error) {
    console.error('Logout API error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
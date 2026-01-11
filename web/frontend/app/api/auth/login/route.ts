import { NextResponse } from "next/server";

export async function POST(req: Request) {
    try{
        const body = await req.json();
        const { email, password } = body;

        const response = await fetch('http://localhost:8888/api/v1/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();

        if(!response.ok){
            return NextResponse.json(
                { error: data.error || 'Login failed' },
                { status: response.status }
            );
        }

        return NextResponse.json(data, { status: 200 });
    }catch(error){
        console.error('Login API error:', error);
            return NextResponse.json(
                { error: 'Internal server error' },
                { status: 500 }
            );
    }
}
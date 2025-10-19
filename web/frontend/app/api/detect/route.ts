import { NextRequest, NextResponse } from 'next/server';

export const config = {
    api: {
        bodyParser: false,
    },
};

interface Result {
    label: string;
    conf: number;
    category: string;
    error?: string | null;
}

export async function POST(req: NextRequest) {
    const backendUrl = 'http://localhost:3000/detect';

    try{

        const formData = await req.formData();
        const image = formData.get('image') as File | null;

        if(!image){
            return NextResponse.json(
                {error: "Missing image file"},
                {status: 400}
            );
        }
        const form = new FormData();
        form.append('image', image)
        const response = await fetch(backendUrl, {
            method: 'POST',
            body: form,
        });

        if(!response.ok){
            return NextResponse.json({ error: 'Backend error' }, { status: response.status });
        }

        const data: Result = await response.json();
        return NextResponse.json({
            ...data
        }, {status: 200});
    }catch(error){
        console.log(error);
        return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
    }
}
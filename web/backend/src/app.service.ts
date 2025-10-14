import { Injectable, Response } from '@nestjs/common';

@Injectable()
export class AppService {

  async runDetection(filePath: string): Promise<{
    label: string | null;
    conf: number | null;
    category: string | null;
    error: string | null;
  }>{
    try{
      const response = await fetch('http://localhost:8000/detect', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({image_path: filePath})
      });

      const res = await response.json()
      // console.log(res);
      if(!response.ok){
        return {
          label: null,
          conf: null,
          category: null,
          error: res.error
        }
      }

      return {
        label: res.Label,
        conf: res.Confident,
        category: res.Category,
        error: null
      }
    } catch(err: any){
      return {
        label: null,
        conf: null,
        category: null,
        error: `Error: ${err.message}`
      }
    }
  }
}
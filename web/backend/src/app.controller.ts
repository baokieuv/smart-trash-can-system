import { Controller, Get, Post, UploadedFile, UseInterceptors, Body, Res, HttpStatus } from '@nestjs/common';
import { Response } from 'express';
import { AppService } from './app.service';
import { FileInterceptor } from '@nestjs/platform-express';
import { diskStorage } from 'multer';
import * as path from 'path';
import * as fs from 'fs';


const storage = diskStorage({
  destination: './uploads/results',
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    const ext = path.extname(file.originalname);
    cb(null, `${file.fieldname}-${uniqueSuffix}${ext}`);
  },
});

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Post('/detect')
  @UseInterceptors(FileInterceptor('image', { storage }))
  async handleUploadCommon(
    @UploadedFile() file: Express.Multer.File,
    @Res() res: Response
  ){
    if(!file){
      return res.status(HttpStatus.BAD_REQUEST).json({
        success: false,
        error: 'Missing image or type',
      });
    }

    const imagePath = path.resolve(file.path);
    // console.log('Image saved at:', imagePath)
    try{
      
      const result = await this.appService.runDetection(imagePath);
      fs.unlink(imagePath, () => {});

      return res.status(HttpStatus.OK).json({
          label: result.label,
          conf: result.conf,
          category: result.category,
          error: result.error
        });

    }catch(error){
      console.error('Detection error:', error);
      fs.unlink(file.path, () => {});
      return res.status(HttpStatus.INTERNAL_SERVER_ERROR).json({
        error: error?.message || 'Unexpected detection error.',
      });
    }
  }
}

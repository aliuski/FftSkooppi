package com.probe.aki.fftskooppi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.probe.aki.fftskooppi.fft.Complex;
import com.probe.aki.fftskooppi.fft.Fft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GraphView extends View {

    static int FFTBUFFERSIZE = 4096;
    private static int FFTBUFFERSHOWSIZE = 2000;
    private static double SQRTBUFFERSIZE = Math.sqrt(FFTBUFFERSIZE);

    private static final int MARGINALSIZE = 20;
    private static final int MARGINALSIZE2 = 40;

    private Paint paint;
    private int sizex;
    private int sizey;

    private double fftresult[] = null;
    private int max_value = 0;
    private int start_frequency = 0;
    private int stop_frequency = FFTBUFFERSHOWSIZE;

    public GraphView(Context context) {
        super(context);
        paint = new Paint();
        paint.setTextSize(18);
    }

    public GraphView(Context context, AttributeSet set) {
        super(context, set);
        paint = new Paint();
        paint.setTextSize(18);
    }

    public void setBundleData(Bundle bundle) {
        if (fftresult != null) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bos);
                oout.writeObject(start_frequency);
                oout.writeObject(stop_frequency);
                oout.writeObject(max_value);
                oout.writeObject(fftresult);
                byte[] yourBytes = bos.toByteArray();
                bundle.putByteArray("savedata", yourBytes);
                oout.close();
                bos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getBundleData(Bundle bundle) {
        try {
            byte sd[] = bundle.getByteArray("savedata");
            if(sd == null)
                return;
            ByteArrayInputStream bis = new ByteArrayInputStream(sd);
            ObjectInputStream ois =
                    new ObjectInputStream(bis);
            start_frequency = (Integer) ois.readObject();
            stop_frequency = (Integer) ois.readObject();
            max_value = (Integer) ois.readObject();
            fftresult = (double[]) ois.readObject();
            bis.close();
            ois.close();
            invalidate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setStartFrequency(int start_frequency) {
        this.start_frequency = start_frequency;
        invalidate();
    }

    public void setStopFrequency(int stop_frequency) {
        this.stop_frequency = stop_frequency;
        invalidate();
    }

    public int getStartFrequency() {
        return start_frequency;
    }

    public int getStopFrequency() {
        return stop_frequency;
    }

    public void setFft(short input[]){
        Complex f[] = new Complex[FFTBUFFERSIZE];
        Complex A[] = new Complex[FFTBUFFERSIZE];
        for(int loop=0 ; loop<FFTBUFFERSIZE ; loop++)
        {
            f[loop] = new Complex((double)input[loop],0);
            A[loop] = new Complex();
        }
        Fft.fft(f, A, FFTBUFFERSIZE);
        if(fftresult==null)
            fftresult = new double[FFTBUFFERSHOWSIZE];
        int temp_max_value = 0;
        for(int loop=0 ; loop<FFTBUFFERSHOWSIZE ; loop++) {
            double tmp = Math.sqrt(A[loop].Re() * A[loop].Re() + A[loop].Im() * A[loop].Im()) / SQRTBUFFERSIZE;
            if(tmp > temp_max_value)
                temp_max_value = (int)tmp;
            fftresult[loop] = tmp;
        }
        max_value = (temp_max_value / 5) * 5 + 5;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int loop;
        sizex = this.getWidth() - MARGINALSIZE;
        sizey = this.getHeight() - MARGINALSIZE2;

        int tempt = (stop_frequency - start_frequency) * 4 / 10;
        double kerroin = (double)(sizex - MARGINALSIZE) / 10;
        for(loop=0;loop<10;loop++){
            int x = (int)((double)loop * kerroin) + MARGINALSIZE;
            paint.setColor(Color.GRAY);
            canvas.drawLine(x,sizey-10,x,sizey,paint);
            paint.setColor(Color.BLACK);
            canvas.drawText(Integer.toString(tempt*loop+start_frequency * 4), x, sizey+15,paint);
        }

        double ty = (double)(sizey - MARGINALSIZE) / 5.0;
        for(loop=0;loop<6;loop++){
            int y = sizey-MARGINALSIZE - (int)((double)loop * ty) + MARGINALSIZE;
            paint.setColor(Color.GRAY);
            canvas.drawLine(MARGINALSIZE,y,sizex,y,paint);
            paint.setColor(Color.BLACK);
            int ms = (int)(max_value / 5.0 * loop);
            canvas.drawText(Integer.toString(ms), 2, y,paint);
        }
        if(fftresult != null) {
            drewFigure(canvas);
        }
    }

    private void drewFigure(Canvas g){
        int fftlenght = stop_frequency - start_frequency;
        double kerroin = (double)(sizex-MARGINALSIZE) / (fftlenght - 1);
        paint.setColor(Color.BLACK);
        float xyw[] = new float[(fftlenght - 1) * 4];
        int l2 = 0;
        for (int loop = 0; loop < fftlenght - 1; loop++) {
            xyw[l2++] = (int) ((double) loop * kerroin) + MARGINALSIZE;
            xyw[l2++] = sizey - (int) ((double) (sizey - MARGINALSIZE) * fftresult[start_frequency + loop] / max_value);
            xyw[l2++] = (int) ((double) (loop + 1) * kerroin) + MARGINALSIZE;
            xyw[l2++] = sizey - (int) ((double) (sizey - MARGINALSIZE) * fftresult[start_frequency + loop + 1] / max_value);
        }
        g.drawLines(xyw, 0, xyw.length, paint);
    }
}

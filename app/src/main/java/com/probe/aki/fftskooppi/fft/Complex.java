package com.probe.aki.fftskooppi.fft;

public class Complex {
    double re;
    double im;

    public Complex()
    {
        SetRe(0);
        SetIm(0);
    }
    public Complex(double r)
    {
        SetRe(r);
        SetIm(0);
    }
    public Complex(double r, double i)
    {
        SetRe(r);
        SetIm(i);
    }
    public Complex add(Complex c)
    {
        return new Complex(re+c.re,im+c.im);
    }
    public Complex sub(Complex c)
    {
        return new Complex(re-c.re,im-c.im);
    }
    public Complex mul(double c)
    {
        return new Complex(re*c,im*c);
    }
    public Complex mul(Complex c)
    {
        return new Complex(re*c.re-im*c.im,re*c.im+im*c.re);
    }
    public Complex div(double c)
    {
        return new Complex(re/c,im/c);
    }
    public Complex div(Complex c)
    {
        double tre = re*c.re-im*c.im;
        double tim = re*c.im+im*c.re;
        double t2re = c.re*c.re-c.im*c.im;
        return new Complex(tre/t2re,tim/t2re);
    }
    public boolean compare(Complex c)
    {
        return c.Re()==Re() && c.Im()==Im();
    }
    public double Im()
    {
        return im;
    }
    public double Re()
    {
        return re;
    }
    public void set(double r)
    {
        SetRe(r);
        SetIm(0);
    }
    public void SetIm(double d)
    {
        im=d;
    }
    public void SetRe(double d)
    {
        re=d;
    }
    public double Norm()
    {
        return Math.sqrt(Re()*Re()+Im()*Im());
    }
}

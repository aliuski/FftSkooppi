package com.probe.aki.fftskooppi.fft;

public class Fft {
    private static int b(int j, int n)
    {
        int m;
        int ret=0;
        for(m=0; m<n; ++m)
        {
            if((j&(1<<m)) != 0)
                ret=ret|(1<<(n-1-m));
        }
        return ret;
    }

    private static int log2(int j)
    {
        int i=0;
        while(j!=1)
        {
            i++;
            j=j>>1;
        }
        return i;
    }

    private static Complex w(double x, int N)
    {
        return new Complex(Math.cos((2*Math.PI/N)*x), Math.sin((2*Math.PI/N)*x));
    }

    private static boolean compareVect(Complex f[], Complex A[])
    {
        for(int loop=0 ; loop < f.length ; loop++)
            if(f[loop] != A[loop])
                return false;
        return true;
    }

    //A=palautusvektori (voi olla sama kuin f)
    public static void fft(Complex f[], Complex A[], int N)
    {
        int j,k=0,m,n;
        Complex u;
        Complex v;
        Complex alpha;

        n=log2(N);

        for(j=0; j<N; ++j) {
            if(!compareVect(f, A)) {
                k=b(j, n);
                A[k]=f[j];
            }
            else {
                k=b(j, n);
                if(k>j) {
                    u=A[k];
                    A[k]=f[j];
                    f[j]=u;
                }
            }
        }

        for(m=1; m<=n; ++m) {
            for(k=0; k<(1<<(m-1));++k) {
                alpha=w((double)k*N/(1<<m), N);
                for(j=0; j<(1<<(n-m)); ++j) {
                    u=A[k+j*(1<<m)];
                    v=alpha.mul(A[(1<<(m-1))+k+j*(1<<m)]);
                    A[k+j*(1<<m)]=u.add(v);
                    A[(1<<(m-1))+k+j*(1<<m)]=u.sub(v);
                }
            }
        }

        for(k=0; k<N; ++k) {
            A[k]=A[k].div(Math.sqrt(N));
        }
    }
}

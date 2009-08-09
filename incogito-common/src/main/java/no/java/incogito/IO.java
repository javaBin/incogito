package no.java.incogito;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P1;
import fj.Unit;
import static fj.Unit.unit;
import fj.control.parallel.Callables;
import fj.data.Either;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IO {

    /*
    TODO: Consider writing runFileToFile :: (a -> b) (InputStream -> Callable a) (b -> OutputStream -> Callable Unit)
     */

    public static class Writers {
        public static F<OutputStream, java.io.Writer> toWriter = new F<OutputStream, java.io.Writer>() {
            public java.io.Writer f(OutputStream outputStream) {
                return new OutputStreamWriter(outputStream);
            }
        };
    }

    public static class ByteArrays {
        public static F<InputStream, Callable<byte[]>> streamToByteArray = new F<InputStream, Callable<byte[]>>() {
            public Callable<byte[]> f(final InputStream inputStream) {
                return new Callable<byte[]>() {
                    public byte[] call() throws Exception {
                        ByteArrayOutputStream data = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024 * 8];

                        int read = inputStream.read(buffer);

                        while (read != -1) {
                            data.write(buffer, 0, read);
                            read = inputStream.read(buffer);
                        }

                        return data.toByteArray();
                    }
                };
            }
        };
    }

    public static class Strings {
        public static F<InputStream, Callable<String>> streamToString = new F<InputStream, Callable<String>>() {
            public Callable<String> f(final InputStream inputStream) {
                return new Callable<String>() {
                    public String call() throws Exception {
                        InputStreamReader reader = new InputStreamReader(inputStream);
                        StringBuilder string = new StringBuilder();
                        char[] buffer = new char[1024 * 8];

                        int read = reader.read(buffer);

                        while (read != -1) {
                            string.append(buffer, 0, read);
                            read = reader.read(buffer);
                        }

                        return string.toString();
                    }
                };
            }
        };

        public static F<String, F<Writer, Callable<Unit>>> stringToWriter = curry(new F2<String, Writer, Callable<Unit>>() {
            public Callable<Unit> f(final String s, final Writer writer) {
                return new Callable<Unit>() {
                    public Unit call() throws Exception {
                        writer.write(s);
                        return unit();
                    }
                };
            }
        });

        public static F<String, F<OutputStream, Callable<Unit>>> stringToStream = curry(new F2<String, OutputStream, Callable<Unit>>() {
            public Callable<Unit> f(final String s, final OutputStream outputStream) {
                return new Callable<Unit>() {
                    public Unit call() throws Exception {
                        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                        writer.write(s);
                        writer.flush();
                        return unit();
                    }
                };
            }
        });
    }

    // -----------------------------------------------------------------------
    // InputStream
    // -----------------------------------------------------------------------

    public static <A> Callable<A> runFileInputStream(final F<InputStream, Callable<A>> callableF, final File file) {
        return new Callable<A>() {
            public A call() throws Exception {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    return callableF.f(inputStream).call();
                }
                finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        };
    }

    public static <A> F<F<InputStream, Callable<A>>, F<File, Callable<A>>> runFileInputStream_(){
        return curry(new F2<F<InputStream, Callable<A>>, File, Callable<A>>() {
            public Callable<A> f(F<InputStream, Callable<A>> inputStreamCallableF, File file) {
                return runFileInputStream(inputStreamCallableF, file);
            }
        });
    }
    
    public static <A> P1<Either<Exception, A>> $runFileInputStream(final F<InputStream, Callable<A>> callableF, final File file) {
        return Callables.either(runFileInputStream(callableF, file));
    }

    // -----------------------------------------------------------------------
    // OutputStream
    // -----------------------------------------------------------------------

    public static <A> Callable<A> runFileOutputStream(final F<OutputStream, Callable<A>> callableF, final File file) {
        return new Callable<A>() {
            public A call() throws Exception {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    return callableF.f(outputStream).call();
                }
                finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        };
    }

    public static <A> F<F<OutputStream, Callable<A>>, F<File, Callable<A>>> runFileOutputStream_(){
        return curry(new F2<F<OutputStream, Callable<A>>, File, Callable<A>>() {
            public Callable<A> f(F<OutputStream, Callable<A>> f, File file) {
                return runFileOutputStream(f, file);
            }
        });
    }

    public static <A> P1<Either<Exception, A>> $runFileOutputStream(final F<OutputStream, Callable<A>> callableF, final File file) {
        return Callables.either(runFileOutputStream(callableF, file));
    }
}

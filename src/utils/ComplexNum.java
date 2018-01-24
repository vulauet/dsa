package utils;

/**
 * Created by vula on 16/08/2017.
 */
public class ComplexNum {
    private int real;
    private int complex;

    public ComplexNum(int real, int complex) {
        this.real = real;
        this.complex = complex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplexNum that = (ComplexNum) o;

        if (real != that.real) return false;
        return complex == that.complex;
    }

    public static ComplexNum add(ComplexNum a, ComplexNum b) {
        return new ComplexNum(a.real + b.real, a.complex + b.complex);
    }

    public static ComplexNum subtract(ComplexNum a, ComplexNum b) {
        return new ComplexNum(a.real - b.real, a.complex - b.complex);
    }

    public static ComplexNum multiply(ComplexNum a, ComplexNum b) {
        return new ComplexNum(a.real * b.real - a.complex * b.complex,
                a.real * b.complex + a.complex * b.real);
    }
}

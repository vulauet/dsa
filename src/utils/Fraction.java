package utils;

import java.security.InvalidParameterException;

/**
 * Created by vula on 16/08/2017.
 */
public class Fraction {
    private int numerator;
    private int denominator;

    public Fraction(int numerator, int denominator) {
        if (denominator == 0) throw new InvalidParameterException("Denominator can't be 0");
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fraction fraction = (Fraction) o;
        return subtract(this, fraction).isZero();
    }

    public static Fraction multiply(Fraction a, Fraction b) {
        return new Fraction(a.getNumerator() * b.getNumerator(),
                a.getDenominator() * b.getDenominator());
    }

    public static Fraction divide(Fraction a, Fraction b) {
        if (b.getNumerator() == 0) throw new InvalidParameterException("Can't divide by 0");
        return multiply(a, new Fraction(b.getDenominator(), b.getNumerator()));
    }

    public static Fraction add(Fraction a, Fraction b) {
        int denom = GCD.LCM(a.getDenominator(), b.getDenominator());
        return new Fraction(denom / a.getDenominator() * a.getNumerator() + denom / b.getDenominator() * b.getNumerator(), denom);
    }

    private static Fraction subtract(Fraction fraction1, Fraction fraction2) {
        return add(fraction1,
                new Fraction(-1 * fraction2.getNumerator(), fraction2.getDenominator()));
    }

    public boolean isZero() {
        return denominator != 0 && numerator == 0;
    }

    public static boolean isInverted(Fraction a, Fraction b) {
        return multiply(a, b).equals(new Fraction(1, 1));
    }

    public void reduce() {
        int gcd = GCD.GCD(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
    }
}

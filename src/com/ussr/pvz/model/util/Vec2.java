package com.ussr.pvz.model.util;

public record Vec2(double x, double y) {

    public Vec2 add(Vec2 v) {
        return new Vec2(this.x + v.x, this.y + v.y);
    }

    public Vec2 sub(Vec2 v) {
        return new Vec2(this.x - v.x, this.y - v.y);
    }

    public Vec2 scale(double d) {
        return new Vec2(this.x * d, this.y * d);
    }

    public Vec2 negate() {
        return new Vec2(-this.x, -this.y);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vec2 normalize() {
        double len = length();
        if (len == 0) return Vec2.zero();
        return new Vec2(this.x / len, this.y / len);
    }

    public double distanceTo(Vec2 v) {
        return this.sub(v).length();
    }

    public double dot(Vec2 v) {
        return this.x * v.x + this.y * v.y;
    }

    public static Vec2 zero() {
        return new Vec2(0, 0);
    }

    public static Vec2 of(double x, double y) {
        return new Vec2(x, y);
    }
}
package com.socksstore.models.socks.enams;

public enum SocksSize {

    XS(new double[]{35.0, 35.5, 36.0, 36.5}), S(new double[]{37.0, 37.5, 38.0, 38.5}), M(new double[]{39.0, 39.5, 40.0, 40.5}), L(new double[]{41.0, 41.5, 42.0, 42.5, 43.0}), XL(new double[]{43.5, 44.0, 44.5, 45.0, 45.5, 46.0, 46.5, 47.0});

    private final double[] size;

    SocksSize(double[] size) {
        this.size = size;
    }

    public double[] getSize() {
        return size;
    }

    public static SocksSize checkFitToSize(double reallySize) {
        SocksSize socksSize = null;
        for (SocksSize socksSizes : SocksSize.values()) {
            for (Double size : socksSizes.getSize()) {
                if (reallySize == size) {
                    socksSize = socksSizes;
                    break;
                }
            }
        }
        return socksSize;
    }

    public String getNameOfSize(){
        return this.name();
    }

}

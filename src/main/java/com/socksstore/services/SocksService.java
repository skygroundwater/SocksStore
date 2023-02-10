package com.socksstore.services;


import com.socksstore.models.socks.Socks;

public interface SocksService {
    void addSocksToStore(Socks socks, Long quantity);
    void removeSocksFromStore(Socks socks, Long quantity);
    Long giveSameSocks(String color, Double size, Integer composition);
}

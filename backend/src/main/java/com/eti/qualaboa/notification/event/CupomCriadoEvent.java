package com.eti.qualaboa.notification.event;

import com.eti.qualaboa.cupom.model.Cupom;

public class CupomCriadoEvent {
    private final Cupom cupom;

    public CupomCriadoEvent(Cupom cupom) {
        this.cupom = cupom;
    }

    public Cupom getCupom() {
        return cupom;
    }
}

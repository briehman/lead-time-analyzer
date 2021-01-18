package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.entity.Merge;
import java.time.Instant;

public class MergeDateStrategy implements DeliveryDateStrategy {

    @Override
    public Instant apply(Merge merge) {
        return merge.getAuthorDate();
    }
}

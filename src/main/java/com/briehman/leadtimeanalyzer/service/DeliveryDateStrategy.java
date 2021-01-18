package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.entity.Merge;
import java.time.Instant;
import java.util.function.Function;

public interface DeliveryDateStrategy extends Function<Merge, Instant> {}

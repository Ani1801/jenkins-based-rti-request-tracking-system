package com.college.rti.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "rti_tracking_sequences")
public class TrackingSequence {

    @Id
    @Column(name = "sequence_year", nullable = false)
    private Integer sequenceYear;

    @Column(name = "current_val", nullable = false)
    private Long currentVal;

    public TrackingSequence() {
    }

    public TrackingSequence(Integer sequenceYear, Long currentVal) {
        this.sequenceYear = sequenceYear;
        this.currentVal = currentVal;
    }

    public Integer getSequenceYear() {
        return sequenceYear;
    }

    public void setSequenceYear(Integer sequenceYear) {
        this.sequenceYear = sequenceYear;
    }

    public Long getCurrentVal() {
        return currentVal;
    }

    public void setCurrentVal(Long currentVal) {
        this.currentVal = currentVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackingSequence that)) return false;
        return Objects.equals(sequenceYear, that.sequenceYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceYear);
    }
}

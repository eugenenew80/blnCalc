package calc.repo.calc;

import calc.entity.calc.BalanceSubstResultHeader;
import calc.entity.calc.enums.BatchStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BalanceSubstResultHeaderRepo extends JpaRepository<BalanceSubstResultHeader, Long> {
    @Procedure(name = "BalanceSubstResultHeader.calcPeValues")
    void calcPeValues(@Param("p_header_id") Long headerId);

    @Procedure(name = "BalanceSubstResultHeader.calcUnbalance")
    void calcUnbalance(@Param("p_header_id") Long headerId);

    List<BalanceSubstResultHeader> findAllByStatus(BatchStatusEnum status);
}

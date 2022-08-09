package tk.lmcurrency.api.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tk.lmcurrency.api.model.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
	List<Currency> findByThisDateBetween(Date start, Date end);

	@Query(value = "SELECT * FROM currency c ORDER BY id DESC LIMIT 1", nativeQuery = true)
	Currency findLastCurrency();
	
	List<Currency> findByThisDate(Date thisDate);
}

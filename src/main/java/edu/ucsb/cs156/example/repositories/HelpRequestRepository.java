package edu.ucsb.cs156.example.repositories;
import org.springframework.stereotype.Repository;
import edu.ucsb.cs156.example.entities.HelpRequest;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface HelpRequestRepository extends CrudRepository<HelpRequest, String> {
 
}

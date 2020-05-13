package my.foo.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import my.foo.batch.domain.Person;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {
    @Autowired
	private JobControlReport jobControlReport;

	@Override
	public Person process(final Person person) throws Exception {
		final String firstName = person.getFirstName().toUpperCase();
		final String lastName = person.getLastName().toUpperCase();
		jobControlReport.Increment();
		final Person processedPerson = new Person(firstName, lastName);
		return processedPerson;
	}
}

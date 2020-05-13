package my.foo.batch;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import my.foo.batch.domain.Person;

/**
 * Maps the provided FieldSet into a Person object.
 *
 * @author Chris Schaefer
 */
public class PersonFieldSetMapper implements FieldSetMapper<Person> {
	@Override
	public Person mapFieldSet(FieldSet fieldSet) {
		String firstName = fieldSet.readString(0);
		String lastName = fieldSet.readString(1);

		return new Person(firstName, lastName);
	}
}

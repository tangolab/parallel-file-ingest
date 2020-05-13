package my.foo.batch;

import org.springframework.stereotype.Component;

@Component
public class JobControlReport {
    private Integer counter = 0;

    public void Increment(){
        counter++;
    }

    public Integer getCounter(){
        return counter;
    }
}
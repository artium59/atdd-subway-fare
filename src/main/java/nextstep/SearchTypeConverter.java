package nextstep;

import nextstep.subway.constant.SearchType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SearchTypeConverter implements Converter<String, SearchType> {

    @Override
    public SearchType convert(String source) {
        return Arrays.stream(SearchType.values())
                .filter(s -> s.getCriteria().equalsIgnoreCase(source))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}

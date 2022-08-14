package nextstep.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.constant.SearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static nextstep.subway.acceptance.LineSteps.지하철_노선에_지하철_구간_생성_요청;
import static nextstep.subway.acceptance.MemberSteps.로그인_되어_있음;
import static nextstep.subway.acceptance.PathSteps.searchType에_따른_두_역의_최단_경로_조회를_요청;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 경로 검색")
class PathAcceptanceTest extends AcceptanceTest {
    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 남부터미널역;
    private Long 이호선;
    private Long 신분당선;
    private Long 삼호선;

    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        교대역 = 지하철역_생성_요청(관리자, "교대역").jsonPath().getLong("id");
        강남역 = 지하철역_생성_요청(관리자, "강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청(관리자, "양재역").jsonPath().getLong("id");
        남부터미널역 = 지하철역_생성_요청(관리자, "남부터미널역").jsonPath().getLong("id");

        이호선 = 지하철_노선_생성_요청("2호선", "green", 교대역, 강남역, 10, 4);
        신분당선 = 지하철_노선_생성_요청("신분당선", "red", 강남역, 양재역, 10, 4);
        삼호선 = 지하철_노선_생성_요청("3호선", "orange", 교대역, 남부터미널역, 2, 10);

        지하철_노선에_지하철_구간_생성_요청(관리자, 삼호선, createSectionCreateParams(남부터미널역, 양재역, 3, 1));
    }

    @DisplayName("두 역의 최단 거리 경로를 조회한다.")
    @ParameterizedTest(name = "{index}: {2}")
    @MethodSource("유저와_최단_거리_경로_요금")
    void findPathByDistance(String email, int price, String message) {
        // when
        String accessToken = 로그인_되어_있음(email, PASSWORD);
        ExtractableResponse<Response> response = searchType에_따른_두_역의_최단_경로_조회를_요청(accessToken, 교대역, 양재역, SearchType.DISTANCE);

        // then
        assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(교대역, 남부터미널역, 양재역);
        assertThat(response.jsonPath().getInt("distance")).isEqualTo(5);
        assertThat(response.jsonPath().getInt("duration")).isEqualTo(11);
        assertThat(response.jsonPath().getInt("fare")).isEqualTo(price);
    }

    @DisplayName("두 역의 최단 시간 경로를 조회한다.")
    @ParameterizedTest(name = "{index}: {2}")
    @MethodSource("유저와_최단_시간_경로_요금")
    void findPathByDuration(String email, int price, String message) {
        // when
        String accessToken = 로그인_되어_있음(email, PASSWORD);
        ExtractableResponse<Response> response = searchType에_따른_두_역의_최단_경로_조회를_요청(accessToken, 교대역, 양재역, SearchType.DURATION);

        // then
        assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(교대역, 강남역, 양재역);
        assertThat(response.jsonPath().getInt("distance")).isEqualTo(20);
        assertThat(response.jsonPath().getInt("duration")).isEqualTo(8);
        assertThat(response.jsonPath().getInt("fare")).isEqualTo(price);
    }

    private static Stream<Arguments> 유저와_최단_거리_경로_요금() {
        return Stream.of(
                Arguments.of("child@email.com", 450, "어린이 사용자는 350원을 공제한 금액의 50%를 할인받는다."),
                Arguments.of("teenager@email.com", 720, "청소년 사용자는 350원을 공제한 금액의 20%를 할인받는다."),
                Arguments.of("member@email.com", 1250, "성인 사용자는 할인 안받는다.")
        );
    }

    private static Stream<Arguments> 유저와_최단_시간_경로_요금() {
        return Stream.of(
                Arguments.of("child@email.com", 550, "어린이 사용자는 350원을 공제한 금액의 50%를 할인받는다."),
                Arguments.of("teenager@email.com", 880, "청소년 사용자는 350원을 공제한 금액의 20%를 할인받는다."),
                Arguments.of("member@email.com", 1450, "성인 사용자는 할인 안받는다.")
        );
    }

    private Long 지하철_노선_생성_요청(String name, String color, Long upStation, Long downStation, int distance, int duration) {
        Map<String, String> lineCreateParams;
        lineCreateParams = new HashMap<>();
        lineCreateParams.put("name", name);
        lineCreateParams.put("color", color);
        lineCreateParams.put("upStationId", upStation + "");
        lineCreateParams.put("downStationId", downStation + "");
        lineCreateParams.put("distance", distance + "");
        lineCreateParams.put("duration", duration + "");

        return LineSteps.지하철_노선_생성_요청(관리자, lineCreateParams).jsonPath().getLong("id");
    }

    private Map<String, String> createSectionCreateParams(Long upStationId, Long downStationId, int distance, int duration) {
        Map<String, String> params = new HashMap<>();
        params.put("upStationId", upStationId + "");
        params.put("downStationId", downStationId + "");
        params.put("distance", distance + "");
        params.put("duration", duration + "");
        return params;
    }
}

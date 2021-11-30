package nextstep.subway.line;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationRequest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class LineSectionAcceptanceTest extends AcceptanceTest {

    private Long 강남역_Id;
    private Long 판교역_Id;
    private Long 정자역_Id;
    private Long 광교역_Id;

    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역_Id = StationAcceptanceTest.지하철_역_등록되어_있음(new StationRequest("강남역"));
        판교역_Id = StationAcceptanceTest.지하철_역_등록되어_있음(new StationRequest("판교역"));
        정자역_Id = StationAcceptanceTest.지하철_역_등록되어_있음(new StationRequest("정자역"));
        광교역_Id = StationAcceptanceTest.지하철_역_등록되어_있음(new StationRequest("광교역"));
    }

    @DisplayName("노선의 구간 사이에 새로운 역을 등록 한다. (상행 종점 일치)")
    @Test
    void addStationBetweenSection1() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 강남역_Id, 정자역_Id, 10));

        // when
        SectionRequest request = new SectionRequest(강남역_Id, 판교역_Id, 6);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, lineLocation);

        // then
        새로운_역_등록됨(response, 판교역_Id);
        지하철_노선에_등록한_구간_포함됨(response, Arrays.asList("강남역", "판교역", "정자역"));
    }

    @DisplayName("노선의 구간 사이에 새로운 역을 등록 한다. (하행 종점 일치)")
    @Test
    void addStationBetweenSection2() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 강남역_Id, 정자역_Id, 10));

        // when
        SectionRequest request = new SectionRequest(판교역_Id, 정자역_Id, 6);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, lineLocation);

        // then
        새로운_역_등록됨(response, 판교역_Id);
        지하철_노선에_등록한_구간_포함됨(response, Arrays.asList("강남역", "판교역", "정자역"));
    }

    @DisplayName("새로운 역을 상행 종점으로 구간을 등록한다.")
    @Test
    void addSectionNewUpstation() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 판교역_Id, 정자역_Id, 5));

        // when
        SectionRequest request = new SectionRequest(강남역_Id, 판교역_Id, 5);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, lineLocation);

        // then
        새로운_역_등록됨(response, 강남역_Id);
        지하철_노선에_등록한_구간_포함됨(response, Arrays.asList("강남역", "판교역", "정자역"));
    }

    @DisplayName("새로운 역을 하행 종점으로 구간을 등록한다.")
    @Test
    void addSectionNewDownStation() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 판교역_Id, 정자역_Id, 5));

        // when
        SectionRequest request = new SectionRequest(정자역_Id, 광교역_Id, 5);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, lineLocation);

        // then
        새로운_역_등록됨(response, 광교역_Id);
        지하철_노선에_등록한_구간_포함됨(response, Arrays.asList("판교역", "정자역", "광교역"));
    }

    @DisplayName("역 사이에 새로운 역을 등록할 때 기존 역 사이 길이보다 크거나 같으면 등록할 수 없다.")
    @Test
    void addSectionDistanceGreaterThanOrEquals() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 강남역_Id, 정자역_Id, 5));

        // when
        SectionRequest sameDistanceRequest = new SectionRequest(강남역_Id, 판교역_Id, 5);
        ExtractableResponse<Response> response1 = 지하철_구간_등록_요청(sameDistanceRequest, lineLocation);

        SectionRequest greaterDistanceRequest = new SectionRequest(강남역_Id, 판교역_Id, 6);
        ExtractableResponse<Response> response2 = 지하철_구간_등록_요청(greaterDistanceRequest,
            lineLocation);

        // then
        새로운_구간_등록_실패됨(response1);
        새로운_구간_등록_실패됨(response2);
    }

    @DisplayName("상행역과 하행역이 이미 노선에 모두 등록되어 있다면 추가할 수 없다.")
    @Test
    void addSectionAlreadyRegisteredAllStations() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 강남역_Id, 정자역_Id, 5));

        // when
        SectionRequest request = new SectionRequest(강남역_Id, 정자역_Id, 6);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, lineLocation);

        // then
        새로운_구간_등록_실패됨(response);
    }

    @DisplayName("상행역과 하행역 둘 중 하나도 포함되어 있지 않으면 추가할 수 없다.")
    @Test
    void addSectionNotRegisteredAllStations() {
        // given
        String lineLocation = LineAcceptanceTest.지하철_노선_등록되어_있음(
            new LineRequest("신분당선", "bg-red-600", 강남역_Id, 판교역_Id, 5));

        // when
        SectionRequest request = new SectionRequest(정자역_Id, 광교역_Id, 6);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청(request, lineLocation);

        // then
        새로운_구간_등록_실패됨(response);
    }

    private void 지하철_노선에_등록한_구간_포함됨(ExtractableResponse<Response> response, List<String> expectedStationNames) {
        List<StationResponse> stations = response.body().jsonPath().getList("stations", StationResponse.class);

        List<String> stationNames = stations.stream()
            .map(StationResponse::getName)
            .collect(Collectors.toList());

        assertThat(stationNames).containsAll(expectedStationNames);
    }

    private void 새로운_구간_등록_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 새로운_역_등록됨(ExtractableResponse<Response> response, Long stationId) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<StationResponse> stations = response.body().jsonPath().getList("stations", StationResponse.class);

        List<Long> stationIds = stations.stream()
            .map(StationResponse::getId)
            .collect(Collectors.toList());

        assertThat(stationIds).contains(stationId);
    }

    private ExtractableResponse<Response> 지하철_구간_등록_요청(SectionRequest request, String linePath) {
        String sectionRegisterPath = linePath + "/sections";
        return RestTestApi.post(request, sectionRegisterPath);
    }

}

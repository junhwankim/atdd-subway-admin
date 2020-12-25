package nextstep.subway.line;

import static nextstep.subway.line.LineTestFixture.*;
import static nextstep.subway.station.StationTestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.BaseTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.dto.StationRequest;
import nextstep.subway.station.dto.StationResponse;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends BaseTest {

	@Autowired
	private StationService stationService;

	private StationResponse exampleStation1;
	private StationResponse exampleStation2;
	private StationResponse exampleStation3;

	private LineResponse exampleLine1;

	@BeforeEach
	void setup() {

		exampleStation1 = stationService.saveStation(StationRequest.of(EXAMPLE_STATION1_NAME));
		exampleStation2 = stationService.saveStation(StationRequest.of(EXAMPLE_STATION2_NAME));
		exampleStation3 = stationService.saveStation(StationRequest.of(EXAMPLE_STATION3_NAME));

		exampleLine1 = requestCreateLine(
			LineRequest.of(EXAMPLE_LINE1_NAME, EXAMPLE_LINE1_COLOR, exampleStation1.getId(), exampleStation2.getId(),
				100)
		).as(LineResponse.class);

		requestCreateLine(
			LineRequest.of(EXAMPLE_LINE2_NAME, EXAMPLE_LINE2_COLOR, exampleStation2.getId(), exampleStation3.getId(),
				200)
		).as(LineResponse.class);
	}

	@DisplayName("지하철 노선을 생성한다.")
	@Test
	void createLine() {
		String name = "3호선";
		String color = "빨간색";
		LineRequest lineRequest = LineRequest.of(name, color, exampleStation1.getId(), exampleStation3.getId(), 300);

		// when
		// 지하철_노선_생성_요청
		ExtractableResponse<Response> response = requestCreateLine(lineRequest);

		// then
		// 지하철_노선_생성됨
		LineResponse lineResponse = response.as(LineResponse.class);

		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
			() -> assertThat(response.header("Location")).startsWith(LINE_URL_PREFIX),
			() -> assertThat(lineResponse.getName()).isEqualTo(name),
			() -> assertThat(lineResponse.getColor()).isEqualTo(color),
			() -> assertThat(lineResponse.getStations()).hasSize(2)
		);
	}

	@DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
	@Test
	void createLine2() {
		// given
		// 지하철_노선_등록되어_있음

		// when
		// 지하철_노선_생성_요청
		ExtractableResponse<Response> response = requestCreateLine(
			LineRequest.of(
				EXAMPLE_LINE1_NAME,
				EXAMPLE_LINE1_COLOR,
				exampleStation1.getId(),
				exampleStation2.getId(),
				200
			)
		);

		// then
		// 지하철_노선_생성_실패됨
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("지하철 노선 목록을 조회한다.")
	@Test
	void getLines() {
		// given
		// 지하철_노선_등록되어_있음
		// 지하철_노선_등록되어_있음

		// when
		// 지하철_노선_목록_조회_요청
		ExtractableResponse<Response> response = requestGetLines();

		// then
		// 지하철_노선_목록_응답됨
		// 지하철_노선_목록_포함됨
		List<LineResponse> lineResponses = response.jsonPath().getList(".", LineResponse.class);
		List<String> lineNames = CollectionUtils.emptyIfNull(lineResponses)
			.stream()
			.map(LineResponse::getName)
			.collect(Collectors.toList());

		boolean isAllHasStation = lineResponses.stream()
			.allMatch(lineResponse -> lineResponse.countStations() > 0);

		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(lineNames).contains(EXAMPLE_LINE1_NAME, EXAMPLE_LINE2_NAME),
			() -> assertThat(isAllHasStation).isTrue()
		);
	}

	@DisplayName("지하철 노선을 조회한다.")
	@Test
	void getLine() {
		// given
		// 지하철_노선_등록되어_있음

		// when
		// 지하철_노선_조회_요청
		ExtractableResponse<Response> response = requestGetLineById(exampleLine1.getId());

		// then
		// 지하철_노선_응답됨
		LineResponse lineResponse = response.as(LineResponse.class);

		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(lineResponse.getName()).isEqualTo(EXAMPLE_LINE1_NAME),
			() -> assertThat(lineResponse.getColor()).isEqualTo(EXAMPLE_LINE1_COLOR),
			() -> assertThat(lineResponse.getStations()).hasSize(2)
		);
	}

	@DisplayName("지하철 노선을 수정한다.")
	@Test
	void updateLine() {
		// given
		// 지하철_노선_등록되어_있음

		// when
		// 지하철_노선_수정_요청
		String changeName = "3호선";
		String changeColor = "핑크색";
		ExtractableResponse<Response> response = requestUpdateLine(
			exampleLine1.getId(),
			LineRequest.of(
				changeName,
				changeColor,
				exampleLine1.findUpStationId(),
				exampleLine1.findDownStationId(),
				100)
		);

		// then
		// 지하철_노선_수정됨
		LineResponse lineResponse = response.as(LineResponse.class);
		List<String> stationNames = lineResponse.getStations()
			.stream()
			.map(StationResponse::getName)
			.collect(Collectors.toList());

		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(lineResponse.getName()).isEqualTo(changeName),
			() -> assertThat(lineResponse.getColor()).isEqualTo(changeColor),
			() -> assertThat(stationNames).contains(EXAMPLE_STATION1_NAME, EXAMPLE_STATION2_NAME)
		);
	}

	@DisplayName("지하철 노선을 제거한다.")
	@Test
	void deleteLine() {
		// given
		// 지하철_노선_등록되어_있음

		// when
		// 지하철_노선_제거_요청
		ExtractableResponse<Response> response = requestDeleteLine(exampleLine1.getId());

		// then
		// 지하철_노선_삭제됨
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
}
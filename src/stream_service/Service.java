package stream_service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import homebook.dao.HomeBookDAO;
import homebook.dao.IDao;
import homebook.tools.ConnectionFactory;
import homebook.vo.HomeBook;
	// 복잡한 질의없이 Stream을 이용한 서비스 개발 예제 
	public class Service {
	private Connection conn;
	private IDao dao;

	public Service() {
		try {
			this.conn = ConnectionFactory.create();
			this.dao = new HomeBookDAO();
			} catch (SQLException e) {

			e.printStackTrace();
			}
	}

	// SERVICE TEST
	public static void main(String[] args) throws SQLException {
		Service service = new Service();
		service.test(service.conn, service.dao);
	}

	// all data 얻는 메소드
	public List<HomeBook> getAllData() {
		List<HomeBook> data = null;
		try {
			data = dao.selectAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	// 지정한 날짜의 자료를 출력해주는 서비스
	public List<HomeBook> getSomedayData(int year, int month, int day) {
		Predicate<HomeBook> pre = h -> {
			Timestamp ts = h.getDay();
			boolean a = ts.getYear() == year - 1900;// 1900을 빼고
			boolean b = ts.getMonth() == month - 1;// 1을 뺌
			boolean c = ts.getDate() == day;
			return a && b && c;
		};
		return getAllData().stream().filter(pre).collect(Collectors.toList());
	}

	// 지정기간의 자료를 출력해주는 서비스
	public List<HomeBook> getTermData(Timestamp a, Timestamp b) {
		Predicate<HomeBook> pre = h -> {
			Timestamp ts = h.getDay();
			return ((ts.after(a) && ts.before(b)) || ts.equals(a) || ts.equals(b));
		};

		return getAllData().stream().filter(pre).collect(Collectors.toList());

	}

	// 지정한 금액 이상의 지출을 한 목록
	public List<HomeBook> moreThanData(long amount) {
		return getAllData().stream().filter(h -> h.getSection().equals("지출")).filter(h -> h.getExpense() >= amount)
				.collect(Collectors.toList());
	}

	// 지정한 계정과목 데이타 목록
	public List<HomeBook> certainAccountTitle(String title) {
		return getAllData().stream().filter(h -> h.getAccounttitle().equals(title)).collect(Collectors.toList());
	}

	// 지정한 계정과목의 합계 얻기
	public long certainAccountTitleSum(String title) {
		return getAllData().stream().filter(h -> h.getAccounttitle().equals(title))
				.mapToLong(h -> (h.getSection().equals("수입")) ? h.getRevenue() : h.getExpense()).sum();
	}

	// 사용된 계정과목 추출하기
	public List<String> getUsedTitle() {
		return getAllData().stream().map(h -> h.getAccounttitle()).distinct().collect(Collectors.toList());
	}

	// 가계부 데이타를 계정과목별로 분류하여 집계출력 하는 서비스
	public void sumPrintService() {
		List<String> usedT = getUsedTitle();
		for (String x : usedT) {
			System.out.println(x + ":" + certainAccountTitleSum(x));
		}

	}

	public void test(Connection conn, IDao dao) {
		getAllData().stream().forEach(x -> System.out.println(x));
		// 차변(수입)합계
		long 차변합계 = getAllData().stream().mapToLong(h -> h.getRevenue()).sum();
		// 대변(지출)합계
		long 대변합계 = getAllData().stream().mapToLong(h -> h.getExpense()).sum();
		// 사용된 지출과목 리스트 얻기
		getUsedTitle().stream().forEach(System.out::println);

		System.out.println("----------- some day data ------------------");
		// 지정한 날짜의 가계부 목록들
		getSomedayData(2021, 6, 25).stream().forEach(d -> System.out.println(d));// System.out::println

		System.out.println("----------- term data  ------------------");
		// 지정한 기간의 가계부 목록들
		Timestamp a = new Timestamp(121, 5, 1, 0, 0, 0, 0);// 2021.6.1
		Timestamp b = new Timestamp(121, 5, 30, 0, 0, 0, 0);// 2021.6.30
		getTermData(a, b).stream().forEach(System.out::println);

		System.out.println("----------- 50000원 이상 지출------------------");
		moreThanData(50000).stream().forEach(System.out::println);

		//
		System.out.println("------------특정과목만 출력하기------------");
		// 주어진 지출 계정 과목만 출력하기
		certainAccountTitle("주식비").forEach(h -> System.out.println(h));

		// 주어진 지출 계정 과목의 합계
		System.out.println("------------특정과목만 합계출력하기------------");
		System.out.println("주식비 지출계:" + certainAccountTitleSum("주식비"));

		// ---------계정과목별 집계
		System.out.println("------------계정과목별 집계 ------------");
		sumPrintService();
	}
}

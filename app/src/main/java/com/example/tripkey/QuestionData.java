package com.example.tripkey;

import com.example.tripkey.Question;

import java.util.Arrays;
import java.util.List;

public class QuestionData {
    public static List<Question> getQuestions() {
        return Arrays.asList(
                new Question("여행할 때 무엇을 더 선호하시나요?", "호텔에서 여유롭게 쉬기", "자연경관 찾아가기"),
                new Question("이동수단으로 어떤 것을 더 선호하시나요?", "대중교통", "편리한 택시"),
                new Question("숙소를 고를 때 어떤 것이 더 중요하시나요?", "최고급 호텔이나 리조트에서의 특별한 경험", "깔끔하고 실용적인 숙소"),
                new Question("어느 활동이 더 끌리시나요?", "현지 음식을 탐방하는 미식 여행", "역사와 문화를 배울 수 있는 박물관 탐방"),
                new Question("어떤 활동이 더 재밌어보이시나요?", "스파 즐기고 카페가기", "드라이브 즐기기"),
                new Question("장거리 이동 시 어떤 것을 선호하시나요?", "직접 운전해서 자유롭게 이동", "걸으면서 천천히 주변을 탐방"),
                new Question("음식을 고를 때 무엇을 선호하시나요?", "고급 레스토랑에서의 식사", "현지 시장이나 저렴한 맛집에서의 식사"),
                new Question("둘 중에 우선순위인 활동은 무엇인가요?", "맛집과 카페 방문", "문화 체험과 전시관람"),
                new Question("보통 어떤 여행을 즐기시나요?", "느긋하게 실내 공간에서 여행 즐기기", "바깥에서 다양한 활동하며 즐기기"),
                new Question("여행지에서 이동할 때 무엇이 더 중요한가요?", "편리함", "비용"),
                new Question("예산을 계획할 때 어떤 방식을 선호하시나요?", "특별한 경험을 위해선 돈이 아깝지 않다", "그래도 가성비 여행이 더 좋다"),
                new Question("여행 중 남는 시간에 하고 싶은 활동은 무엇인가요?", "유명한 현지 음식 즐기기", "유명한 관광지 둘러보기")
        );
    }
}

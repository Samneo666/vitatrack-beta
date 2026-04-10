package web.member_admin.dto;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResultResponse<T> {
	private List<T> content;
    private long totalNumber;
    private int totalPages;
    private int currentPage;
}

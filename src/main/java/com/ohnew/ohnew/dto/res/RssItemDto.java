package com.ohnew.ohnew.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RssItemDto {
    private String title;
    private String guid;
    private String category;
    private String textAreaContent;
}

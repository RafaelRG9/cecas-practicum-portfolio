package edu.franklin.cecas.dto;

public class ExtraCreditRequestCreateDTO {
    private Integer courseId;
    private Integer categoryId;
    private String description;

    public ExtraCreditRequestCreateDTO() {}

        public Integer getCourseId() {
            return courseId;
        }

        public void setCourseId(Integer courseId) {
            this.courseId = courseId;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
}

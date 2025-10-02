using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddDB3 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Properties_PropertyCategories_CategoryId",
                table: "Properties");

            migrationBuilder.AlterColumn<int>(
                name: "CategoryId",
                table: "Properties",
                type: "int",
                nullable: true,
                oldClrType: typeof(int),
                oldType: "int");

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_PropertyCategories_CategoryId",
                table: "Properties",
                column: "CategoryId",
                principalTable: "PropertyCategories",
                principalColumn: "CategoryId",
                onDelete: ReferentialAction.Restrict);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Properties_PropertyCategories_CategoryId",
                table: "Properties");

            migrationBuilder.AlterColumn<int>(
                name: "CategoryId",
                table: "Properties",
                type: "int",
                nullable: false,
                defaultValue: 0,
                oldClrType: typeof(int),
                oldType: "int",
                oldNullable: true);

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_PropertyCategories_CategoryId",
                table: "Properties",
                column: "CategoryId",
                principalTable: "PropertyCategories",
                principalColumn: "CategoryId",
                onDelete: ReferentialAction.Cascade);
        }
    }
}

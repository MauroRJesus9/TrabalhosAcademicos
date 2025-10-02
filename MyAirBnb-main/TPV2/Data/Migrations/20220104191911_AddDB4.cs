using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddDB4 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Score",
                table: "Reserves");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "Score",
                table: "Reserves",
                type: "int",
                nullable: false,
                defaultValue: 0);
        }
    }
}

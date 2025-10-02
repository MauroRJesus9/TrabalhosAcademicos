using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddDB5 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Properties_AspNetUsers_FuncionarioId",
                table: "Properties");

            migrationBuilder.DropIndex(
                name: "IX_Properties_FuncionarioId",
                table: "Properties");

            migrationBuilder.DropColumn(
                name: "FuncionarioId",
                table: "Properties");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "FuncionarioId",
                table: "Properties",
                type: "nvarchar(450)",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Properties_FuncionarioId",
                table: "Properties",
                column: "FuncionarioId");

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_AspNetUsers_FuncionarioId",
                table: "Properties",
                column: "FuncionarioId",
                principalTable: "AspNetUsers",
                principalColumn: "Id",
                onDelete: ReferentialAction.Restrict);
        }
    }
}

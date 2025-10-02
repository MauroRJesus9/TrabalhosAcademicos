using Microsoft.EntityFrameworkCore.Migrations;

namespace TPV2.Data.Migrations
{
    public partial class AddDB6 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "FuncionarioId",
                table: "Properties",
                type: "nvarchar(450)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ProprietarioId",
                table: "Properties",
                type: "nvarchar(450)",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Properties_FuncionarioId",
                table: "Properties",
                column: "FuncionarioId");

            migrationBuilder.CreateIndex(
                name: "IX_Properties_ProprietarioId",
                table: "Properties",
                column: "ProprietarioId");

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_AspNetUsers_FuncionarioId",
                table: "Properties",
                column: "FuncionarioId",
                principalTable: "AspNetUsers",
                principalColumn: "Id",
                onDelete: ReferentialAction.Restrict);

            migrationBuilder.AddForeignKey(
                name: "FK_Properties_AspNetUsers_ProprietarioId",
                table: "Properties",
                column: "ProprietarioId",
                principalTable: "AspNetUsers",
                principalColumn: "Id",
                onDelete: ReferentialAction.Restrict);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Properties_AspNetUsers_FuncionarioId",
                table: "Properties");

            migrationBuilder.DropForeignKey(
                name: "FK_Properties_AspNetUsers_ProprietarioId",
                table: "Properties");

            migrationBuilder.DropIndex(
                name: "IX_Properties_FuncionarioId",
                table: "Properties");

            migrationBuilder.DropIndex(
                name: "IX_Properties_ProprietarioId",
                table: "Properties");

            migrationBuilder.DropColumn(
                name: "FuncionarioId",
                table: "Properties");

            migrationBuilder.DropColumn(
                name: "ProprietarioId",
                table: "Properties");
        }
    }
}

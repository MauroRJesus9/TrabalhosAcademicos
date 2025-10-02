using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HabitAqui.Data.Migrations
{
    public partial class HabitacoesChangeTypeOfCategoria : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder){
            migrationBuilder.DropColumn(
                name: "Categoria",
                table: "Habitacoes");

            migrationBuilder.AddColumn<int>(
                name: "CategoriaId",
                table: "Habitacoes",
                type: "int",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Habitacoes_CategoriaId",
                table: "Habitacoes",
                column: "CategoriaId");

            migrationBuilder.AddForeignKey(
                name: "FK_Habitacoes_Categorias_CategoriaId",
                table: "Habitacoes",
                column: "CategoriaId",
                principalTable: "Categorias",
                principalColumn: "Id");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Habitacoes_Categorias_CategoriaId",
                table: "Habitacoes");

            migrationBuilder.DropIndex(
                name: "IX_Habitacoes_CategoriaId",
                table: "Habitacoes");

            migrationBuilder.DropColumn(
                name: "CategoriaId",
                table: "Habitacoes");

            migrationBuilder.AddColumn<string>(
                name: "Categoria",
                table: "Habitacoes",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");
        }
    }
}

using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HabitAqui.Data.Migrations
{
    public partial class EstadoHabitacaoAdd : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Arrendamentos_EstadoHabitacao_EstadoInicalId",
                table: "Arrendamentos");

            migrationBuilder.DropIndex(
                name: "IX_Arrendamentos_EstadoInicalId",
                table: "Arrendamentos");

            migrationBuilder.DropColumn(
                name: "EstadoInicalId",
                table: "Arrendamentos");

            migrationBuilder.CreateIndex(
                name: "IX_Arrendamentos_EstadoInicialId",
                table: "Arrendamentos",
                column: "EstadoInicialId");

            migrationBuilder.AddForeignKey(
                name: "FK_Arrendamentos_EstadoHabitacao_EstadoInicialId",
                table: "Arrendamentos",
                column: "EstadoInicialId",
                principalTable: "EstadoHabitacao",
                principalColumn: "Id");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Arrendamentos_EstadoHabitacao_EstadoInicialId",
                table: "Arrendamentos");

            migrationBuilder.DropIndex(
                name: "IX_Arrendamentos_EstadoInicialId",
                table: "Arrendamentos");

            migrationBuilder.AddColumn<int>(
                name: "EstadoInicalId",
                table: "Arrendamentos",
                type: "int",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Arrendamentos_EstadoInicalId",
                table: "Arrendamentos",
                column: "EstadoInicalId");

            migrationBuilder.AddForeignKey(
                name: "FK_Arrendamentos_EstadoHabitacao_EstadoInicalId",
                table: "Arrendamentos",
                column: "EstadoInicalId",
                principalTable: "EstadoHabitacao",
                principalColumn: "Id");
        }
    }
}
